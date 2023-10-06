FROM openjdk:17-alpine
WORKDIR /app

# Add it built from outside docker for now because I am LAZY
ADD build/distributions/Kira-2.1.1.tar /app
ENTRYPOINT /app/Kira-2.1.1/bin/Kira
