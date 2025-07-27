#!/bin/bash

# SSL 인증서 생성 스크립트
echo "SSL 인증서 생성 중..."

# 개인키 생성
openssl genrsa -out server-key.pem 2048

# 인증서 서명 요청(CSR) 생성
openssl req -new -key server-key.pem -out server.csr -subj "/C=KR/ST=Seoul/L=Seoul/O=Development/CN=localhost"

# 자체 서명된 인증서 생성
openssl x509 -req -days 365 -in server.csr -signkey server-key.pem -out server-cert.pem

# CSR 파일 삭제
rm server.csr

echo "SSL 인증서 생성 완료!"
echo "생성된 파일:"
echo "- server-cert.pem (인증서)"
echo "- server-key.pem (개인키)" 