"""Exercise Google initialization and reject forged credentials on the local runtime."""
import http.cookiejar
import json
import urllib.error
import urllib.parse
import urllib.request

base = 'http://localhost:8080/plagiarism/login-google'
jar = http.cookiejar.CookieJar()
client = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(jar))
with client.open(base) as response:
    assert response.status == 200
    assert response.headers['Cache-Control'] == 'no-store'
    config = json.load(response)
assert config['clientId'].endswith('.apps.googleusercontent.com')
assert len(config['nonce']) == 43

def reject(data, expected):
    try:
        client.open(base, urllib.parse.urlencode(data).encode())
        raise AssertionError('Unexpected credential acceptance')
    except urllib.error.HTTPError as error:
        assert error.code == expected, error.code
    assert not any(cookie.name == 'AUTH_TOKEN' for cookie in jar)

reject({'credential': 'not-a-token'}, 403)
reject({'credential': 'not-a-token', 'state': config['nonce']}, 401)
reject({'credential': 'not-a-token', 'state': config['nonce']}, 403)
print('PASS: configuration, session nonce, missing state, forged token, replay, no auth cookie')
