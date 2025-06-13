# Jet Kotlin payments

Code used in a demonstration, not intended for general use.

Summary:

- A simple pipeline on Jet, written in Kotlin

- Real time ingest of payment requests from Kafka, and processing of those on Jet

- Automatic grouping of each merchant’s payments to the same node

- Automatic scaling upon failure conditions

- Zero loss and exactly-once semantics
