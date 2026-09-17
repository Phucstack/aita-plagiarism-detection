package com.aita.plagiarism.controller;

import com.aita.plagiarism.dao.UserDAO;
import com.aita.plagiarism.model.User;
import com.aita.plagiarism.service.GoogleIdentityVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import jakarta.servlet.http.*;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import static org.mockito.Mockito.*;

class GoogleLoginServletTest {
    @Test void forgedPostCannotCreateSessionOrCookie() throws Exception {
        var req = mock(HttpServletRequest.class); var res = mock(HttpServletResponse.class);
        when(req.getParameter("credential")).thenReturn("header.payload.fake");
        new GoogleLoginServlet().doPost(req,res);
        verify(res).sendError(403); verify(res,never()).addCookie(any()); verify(req,never()).getSession();
    }
    @Test void validGoogleIdentityUsesDatabaseRoleAndConsumesChallenge() throws Exception {
        var req=mock(HttpServletRequest.class);var res=mock(HttpServletResponse.class);var session=mock(HttpSession.class);
        var state=new HashMap<String,Object>();state.put("googleLoginChallenge","challenge");state.put("googleLoginIssued",System.currentTimeMillis());
        when(session.getAttribute(anyString())).thenAnswer(i->state.get(i.getArgument(0)));
        doAnswer(i->{state.remove(i.getArgument(0));return null;}).when(session).removeAttribute(anyString());
        when(req.getSession(false)).thenReturn(session);when(req.getSession()).thenReturn(session);
        when(req.getContextPath()).thenReturn("/plagiarism");when(req.getParameter("state")).thenReturn("challenge");
        when(req.getParameter("credential")).thenReturn("signed-token");when(req.getParameter("role")).thenReturn("ADMIN");
        var payload=new GoogleIdToken.Payload();payload.setSubject("123");payload.setEmail("student@gmail.com");payload.setEmailVerified(true);
        var user=new User(4,"student","Student","student@gmail.com","STUDENT");
        try(var verifier=mockConstruction(GoogleIdentityVerifier.class,(m,c)->when(m.verify("signed-token","challenge")).thenReturn(payload));
            var dao=mockConstruction(UserDAO.class,(m,c)->when(m.authenticateGoogle("123","student@gmail.com",true)).thenReturn(user))) {
            var servlet=new GoogleLoginServlet();servlet.doPost(req,res);
            verify(req).changeSessionId();verify(res).sendRedirect("/plagiarism/student-portal");
            verify(session).setAttribute("currentUser",user);verify(res).addCookie(argThat(c->"AUTH_TOKEN".equals(c.getName())&&c.isHttpOnly()));
            servlet.doPost(req,res);verify(res).sendError(403);
            verify(verifier.constructed().get(0),times(1)).verify(anyString(),anyString());
        }
    }
    @Test void expiredChallengeNeverReachesVerifier() throws Exception {
        var req=mock(HttpServletRequest.class);var res=mock(HttpServletResponse.class);var session=mock(HttpSession.class);
        when(req.getSession(false)).thenReturn(session);when(req.getParameter("state")).thenReturn("old");
        when(session.getAttribute("googleLoginChallenge")).thenReturn("old");
        when(session.getAttribute("googleLoginIssued")).thenReturn(System.currentTimeMillis()-600_000);
        try(var verifier=mockConstruction(GoogleIdentityVerifier.class)) {
            new GoogleLoginServlet().doPost(req,res);verify(res).sendError(403);verifyNoInteractions(verifier.constructed().get(0));
        }
    }
}
