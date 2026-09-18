# Docker (tùy chọn cho demo tuần 4-5, không thay thế Tomcat local tuần 1-3)

Docker chỉ đóng gói WAR đã build từ `target/aita-plagiarism-detection-1.0.0-SNAPSHOT.war`.
Không dùng `sa` để chạy web; SQL Server trong compose chỉ phục vụ khởi tạo DB local.

1. Build WAR: `$env:MAVEN_OPTS="$env:MAVEN_OPTS -Dfile.encoding=UTF-8"; mvn -B package -DskipTests`
2. Tạo file `.env.docker` (không commit) theo mẫu:
   `DB_NAME=AITA_PlagiarismDB`, `DB_USER`/`DB_PASSWORD` là tài khoản DB quyền hạn chế,
   `JWT_SECRET` ngẫu nhiên tối thiểu 32 byte, `GOOGLE_CLIENT_ID` nếu dùng login Google,
   `MSSQL_SA_PASSWORD` mạnh chỉ dùng local cho container SQL Server.
3. Chạy: `docker compose --env-file .env.docker up --build`
4. Mở `http://localhost:8080/plagiarism/login`.
5. Sau khi SQL Server healthy, chạy `database/database_schema.sql` một lần để tạo schema còn thiếu.
