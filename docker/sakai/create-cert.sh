#!/bin/bash
# 
# This creates a self signed certificate which can be used by Apache for serving HTTPS
# 

openssl req -sha256 -x509 -nodes -newkey rsa:2048 \
  -subj '/C=GB/ST=Oxfordshire/L=Oxford/O=University of Oxford/OU=IT Services/CN=localhost' \
  -keyout ssl-private.key -out ssl-public.crt -days 375


