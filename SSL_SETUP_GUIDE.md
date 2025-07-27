# SSL 핸드셰이크 완전 구현 가이드

## 핵심 원칙
**SSL 핸드셰이크를 반드시 완료해야 합니다!**
- HTTP 폴백 없이 SSL만 사용
- SSL 인증서 필수
- 핸드셰이크 완료 후 데이터 전송

## 1. SSL 인증서 생성

### Windows에서 실행:
```cmd
generate-ssl-cert.bat
```

### 또는 수동으로 생성:
```bash
# 개인키 생성
openssl genrsa -out server-key.pem 2048

# 인증서 서명 요청 생성
openssl req -new -key server-key.pem -out server.csr -subj "/C=KR/ST=Seoul/L=Seoul/O=Development/CN=localhost"

# 자체 서명된 인증서 생성
openssl x509 -req -days 365 -in server.csr -signkey server-key.pem -out server-cert.pem

# CSR 파일 삭제
rm server.csr
```

## 2. 서버 실행

### HTTPS 서버 실행 (SSL 핸드셰이크 필수):
```bash
# 프로젝트 루트 디렉토리에서
mvn clean compile exec:java -Dexec.mainClass="serverManager.ServerManager"
```

**중요**: SSL 인증서가 없으면 서버가 시작되지 않습니다!

## 3. Android 클라이언트 설정

### SSL 인증서 파일 복사:
- `server-cert.pem` 파일을 Android 프로젝트의 `app/src/main/assets/` 폴더에 복사

### Android 코드 수정:
```java
// NettyClient.java에서 인증서 경로 수정
sslCtx = SslContextBuilder.forClient()
    .trustManager(new File(getAssets().openFd("server-cert.pem").getFileDescriptor()))
    .build();
```

## 4. SSL 핸드셰이크 문제 해결

### SSL 핸드셰이크 실패 시:
1. **인증서 파일 확인**: `server-cert.pem`, `server-key.pem` 파일이 프로젝트 루트에 있는지 확인
2. **서버 로그 확인**: "SSL 컨텍스트 생성 성공" 메시지 확인
3. **클라이언트 로그 확인**: "SSL 컨텍스트 초기화 성공" 메시지 확인
4. **핸드셰이크 완료 확인**: "SSL 핸드셰이크 완료" 메시지 확인

### SSL 핸드셰이크 과정:
1. 클라이언트 → 서버: ClientHello
2. 서버 → 클라이언트: ServerHello + Certificate + ServerKeyExchange
3. 클라이언트 → 서버: ClientKeyExchange + ChangeCipherSpec + Finished
4. 서버 → 클라이언트: ChangeCipherSpec + Finished
5. **핸드셰이크 완료 후 데이터 전송**

## 5. SSL 핸드셰이크 강제 구현

### 핵심 변경사항:
- HTTP 폴백 완전 제거
- SSL 인증서 없으면 예외 발생
- SSL 핸들러 필수 추가
- 핸드셰이크 완료 후 데이터 전송

### 코드 특징:
```java
// SSL 핸들러 추가 (반드시 필요)
if (sslCtx != null) {
    p.addLast(sslCtx.newHandler(ch.alloc(), SERVER_HOST, SERVER_PORT));
} else {
    throw new RuntimeException("SSL 컨텍스트가 없습니다");
}
``` 