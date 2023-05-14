FROM openjdk:17-alpine
WORKDIR /app

# Add it built from outside docker for now because I am LAZY
ADD build/distributions/Kira-2.0.2.tar /app
ENTRYPOINT /app/Kira-2.0.2/bin/Kira
