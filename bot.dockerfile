FROM openjdk:17-oracle

COPY ./build/betternames.jar .

CMD java -jar betternames.jar [your token here]
