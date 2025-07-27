@echo off
echo SSL 인증서 생성 중...

REM Java로 간단한 SSL 인증서 생성
echo public class SSLCertGenerator { > SSLCertGenerator.java
echo     public static void main(String[] args) { >> SSLCertGenerator.java
echo         try { >> SSLCertGenerator.java
echo             java.security.KeyPairGenerator kpg = java.security.KeyPairGenerator.getInstance("RSA"); >> SSLCertGenerator.java
echo             kpg.initialize(2048); >> SSLCertGenerator.java
echo             java.security.KeyPair kp = kpg.generateKeyPair(); >> SSLCertGenerator.java
echo             java.io.FileOutputStream fos = new java.io.FileOutputStream("server-key.pem"); >> SSLCertGenerator.java
echo             fos.write("-----BEGIN PRIVATE KEY-----\n".getBytes()); >> SSLCertGenerator.java
echo             fos.write(java.util.Base64.getEncoder().encodeToString(kp.getPrivate().getEncoded()).getBytes()); >> SSLCertGenerator.java
echo             fos.write("\n-----END PRIVATE KEY-----\n".getBytes()); >> SSLCertGenerator.java
echo             fos.close(); >> SSLCertGenerator.java
echo             System.out.println("SSL 인증서 생성 완료!"); >> SSLCertGenerator.java
echo         } catch (Exception e) { >> SSLCertGenerator.java
echo             e.printStackTrace(); >> SSLCertGenerator.java
echo         } >> SSLCertGenerator.java
echo     } >> SSLCertGenerator.java
echo } >> SSLCertGenerator.java

javac SSLCertGenerator.java
java SSLCertGenerator

del SSLCertGenerator.java
del SSLCertGenerator.class

echo SSL 인증서 생성 완료!
echo 생성된 파일:
echo - server-key.pem (개인키) 