import typing

from hazelcast.serialization.api import CompactSerializer, CompactReader, CompactWriter, CompactSerializableType


class Card:
    def __init__(self, card_number: str):
        self.card_number = card_number


class CardSerializer(CompactSerializer[Card]):
    def read(self, reader: CompactReader) -> Card:
        card_number = reader.read_string("cardNumber")
        return Card(card_number)

    def write(self, writer: CompactWriter, card: Card) -> None:
        writer.write_string("cardNumber", card.card_number)

    def get_type_name(self) -> str:
        return "hazelcast.platform.labs.payments.domain.Card"

    def get_class(self) -> typing.Type[CompactSerializableType]:
        return Card
