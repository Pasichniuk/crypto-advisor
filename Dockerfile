FROM openjdk:11-jdk

ARG HOME=/opt/crypto-advisor
ARG USERNAME=crypto-advisor

# Create a base directory
RUN mkdir -p $HOME

# set timezone to the known value for clarity

RUN ln -sf /usr/share/zoneinfo/UTC /etc/localtime

# create an unprivileged user for the application

RUN groupadd -r $USERNAME
RUN useradd -l -r -g $USERNAME $USERNAME
RUN chown -R $USERNAME:$USERNAME $HOME

# create a volume for temporary files

VOLUME /tmp

# copy the build artifacts

COPY prices $HOME/prices/
COPY build/libs/crypto-advisor-0.0.1-SNAPSHOT.jar $HOME/app.jar

USER $USERNAME
WORKDIR $HOME

EXPOSE 8080

ENTRYPOINT [ "java", "-jar", "app.jar" ]