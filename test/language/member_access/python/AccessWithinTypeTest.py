import unittest
import zserio

from testutils import getZserioApi

class AccessWithinTypeTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.api = getZserioApi(__file__, "member_access.zs").access_within_type

    def testRead(self):
        numSentences = 10
        wrongArrayLength = False
        writer = zserio.BitStreamWriter()
        self._writeMessageToStream(writer, numSentences, wrongArrayLength)
        reader = zserio.BitStreamReader(writer.getByteArray())
        message = self.api.Message.fromReader(reader)
        self._checkMessage(message, numSentences)

    def testReadWrongArrayLength(self):
        numSentences = 10
        wrongArrayLength = True
        writer = zserio.BitStreamWriter()
        self._writeMessageToStream(writer, numSentences, wrongArrayLength)
        reader = zserio.BitStreamReader(writer.getByteArray())
        with self.assertRaises(zserio.PythonRuntimeException):
            message = self.api.Message.fromReader(reader)
            self._checkMessage(message, numSentences)

    def testWrite(self):
        numSentences = 13
        wrongArrayLength = False
        message = self._createMessage(numSentences, wrongArrayLength)
        writer = zserio.BitStreamWriter()
        message.write(writer)
        reader = zserio.BitStreamReader(writer.getByteArray())
        readMessage = self.api.Message.fromReader(reader)
        self._checkMessage(readMessage, numSentences)
        self.assertTrue(message == readMessage)

    def testWriteWrongArrayLength(self):
        numSentences = 13
        wrongArrayLength = True
        message = self._createMessage(numSentences, wrongArrayLength)
        writer = zserio.BitStreamWriter()
        with self.assertRaises(zserio.PythonRuntimeException):
            message.write(writer)

    def _writeMessageToStream(self, writer, numSentences, wrongArrayLength):
        writer.writeBits(self.VERSION_VALUE, 16)
        writer.writeBits(numSentences, 16)
        numStrings = numSentences - 1 if wrongArrayLength else numSentences
        for i in range(numStrings):
            writer.writeString(self.SENTENCE_PREFIX + str(i))

    def _checkMessage(self, message, numSentences):
        self.assertEqual(self.VERSION_VALUE, message.getHeader().getVersion())
        self.assertEqual(numSentences, message.getHeader().getNumSentences())

        sentences = message.getSentences()
        self.assertEqual(numSentences, len(sentences))
        for i in range(numSentences):
            expectedSentence = self.SENTENCE_PREFIX + str(i)
            self.assertTrue(sentences[i] == expectedSentence)

    def _createMessage(self, numSentences, wrongArrayLength):
        header = self.api.Header.fromFields(self.VERSION_VALUE, numSentences)
        numStrings = numSentences - 1 if wrongArrayLength else numSentences
        sentences = [self.SENTENCE_PREFIX + str(i) for i in range(numStrings)]

        return self.api.Message.fromFields(header, sentences)

    VERSION_VALUE = 0xAB
    SENTENCE_PREFIX = "This is sentence #"
