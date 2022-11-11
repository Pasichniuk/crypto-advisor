# Crypto Advisor

## Available endpoints:

#### `/` - home page;
#### `/stats` - returns a descending sorted list of all the cryptos, comparing the normalized range;
#### `/stats/{cryptoSymbol}` - return the oldest/newest/min/max values for a requested crypto;
#### `/stats/best` - returns the crypto with the highest normalized range.

## Steps to run in Docker:
#### 1) `./gradlew clean build` 
#### 2) `./gradlew build` 
#### 3) `docker build -t crypto-advisor .` 
#### 4) `docker run -p 8080:8080 crypto-advisor` 