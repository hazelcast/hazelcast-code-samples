FROM golang:1.16-alpine AS builder

WORKDIR /root
COPY go.mod go.sum /root/
COPY cmd/ /root/cmd/
RUN CGO_ENABLED=0 GOOS=linux go build -a -installsuffix cgo ./cmd/client

FROM alpine:latest
RUN apk --no-cache add ca-certificates
COPY --from=builder /root/client .
CMD ["./client"]

