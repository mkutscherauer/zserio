#include "gtest/gtest.h"

#include "zserio/BitStreamWriter.h"
#include "zserio/BitStreamReader.h"

#include "structure_types/one_string_structure/OneStringStructure.h"

namespace structure_types
{
namespace one_string_structure
{

class OneStringStructureTest : public ::testing::Test
{
protected:
    void writeOneStringStructureToByteArray(zserio::BitStreamWriter& writer, const char* oneString)
    {
        writer.writeString(oneString);
    }

    static const size_t EMPTY_ONE_STRING_STRUCTURE_BIT_SIZE;

    static const char ONE_STRING[];
    static const size_t ONE_STRING_STRUCTURE_BIT_SIZE;
};

const char   OneStringStructureTest::ONE_STRING[] = "This is a string!";
const size_t OneStringStructureTest::ONE_STRING_STRUCTURE_BIT_SIZE = (1 + sizeof(ONE_STRING) - 1) * 8;

TEST_F(OneStringStructureTest, emptyConstructor)
{
    OneStringStructure oneStringStructure;
    ASSERT_EQ("", oneStringStructure.getOneString());
}

TEST_F(OneStringStructureTest, fieldConstructor)
{
    {
        const char* str = "test";
        OneStringStructure oneStringStructure(str);
        ASSERT_EQ(str, oneStringStructure.getOneString());
    }

    {
        std::string movedString(1000, 'a'); // long enough to prevent small string optimization
        const void* ptr = movedString.data();
        OneStringStructure oneStringStructure(std::move(movedString));
        const void* movedPtr= oneStringStructure.getOneString().data();
        ASSERT_EQ(ptr, movedPtr);
    }
}

TEST_F(OneStringStructureTest, bitStreamReaderConstructor)
{
    zserio::BitStreamWriter writer;
    writeOneStringStructureToByteArray(writer, ONE_STRING);
    size_t writeBufferByteSize;
    const uint8_t* writeBuffer = writer.getWriteBuffer(writeBufferByteSize);
    zserio::BitStreamReader reader(writeBuffer, writeBufferByteSize);
    OneStringStructure oneStringStructure(reader);
    ASSERT_EQ(ONE_STRING, oneStringStructure.getOneString());
}

// we shall test also generated 'default' methods!
TEST_F(OneStringStructureTest, copyConstructor)
{
    const char* str = "test";
    OneStringStructure oneStringStructure(str);
    OneStringStructure oneStringStructureCopy(oneStringStructure);
    ASSERT_EQ(str, oneStringStructure.getOneString());
    ASSERT_EQ(str, oneStringStructureCopy.getOneString());
}

// we shall test also generated 'default' methods!
TEST_F(OneStringStructureTest, copyAssignmentOperator)
{
    const char* str = "test";
    OneStringStructure oneStringStructure(str);
    OneStringStructure oneStringStructureCopy;
    oneStringStructureCopy = oneStringStructure;
    ASSERT_EQ(str, oneStringStructure.getOneString());
    ASSERT_EQ(str, oneStringStructureCopy.getOneString());
}

// we shall test also generated 'default' methods!
TEST_F(OneStringStructureTest, moveConstructor)
{;
    OneStringStructure oneStringStructure(std::string(1000, 'a'));
    const void* ptr = oneStringStructure.getOneString().data();
    OneStringStructure movedOneStringStructure(std::move(oneStringStructure));
    const void* movedPtr = movedOneStringStructure.getOneString().data();
    ASSERT_EQ(ptr, movedPtr);
    ASSERT_EQ(std::string(1000, 'a'), movedOneStringStructure.getOneString());
}

// we shall test also generated 'default' methods!
TEST_F(OneStringStructureTest, moveAssignmentOperator)
{;
    OneStringStructure oneStringStructure(std::string(1000, 'a'));
    const void* ptr = oneStringStructure.getOneString().data();
    OneStringStructure movedOneStringStructure;
    movedOneStringStructure = std::move(oneStringStructure);
    const void* movedPtr = movedOneStringStructure.getOneString().data();
    ASSERT_EQ(ptr, movedPtr);
    ASSERT_EQ(std::string(1000, 'a'), movedOneStringStructure.getOneString());
}

TEST_F(OneStringStructureTest, getSetOneString)
{
    OneStringStructure oneStringStructure;
    oneStringStructure.setOneString(ONE_STRING);
    ASSERT_EQ(ONE_STRING, oneStringStructure.getOneString());

    std::string movedString(1000, 'a'); // long enough to prevent small string optimization
    const void* ptr = movedString.data();
    oneStringStructure.setOneString(std::move(movedString));
    const void* movedPtr = oneStringStructure.getOneString().data();
    ASSERT_EQ(ptr, movedPtr);

    std::string& value = oneStringStructure.getOneString();
    value = ONE_STRING;
    ASSERT_EQ(ONE_STRING, oneStringStructure.getOneString());
}

TEST_F(OneStringStructureTest, bitSizeOf)
{
    OneStringStructure oneStringStructure;
    oneStringStructure.setOneString(ONE_STRING);
    ASSERT_EQ(ONE_STRING_STRUCTURE_BIT_SIZE, oneStringStructure.bitSizeOf());
}

TEST_F(OneStringStructureTest, initializeOffsets)
{
    OneStringStructure oneStringStructure;
    oneStringStructure.setOneString(ONE_STRING);
    const size_t bitPosition = 1;
    const size_t expectedBitSize = bitPosition + ONE_STRING_STRUCTURE_BIT_SIZE;
    ASSERT_EQ(expectedBitSize, oneStringStructure.initializeOffsets(bitPosition));
}

TEST_F(OneStringStructureTest, operatorEquality)
{
    OneStringStructure oneStringStructure1;
    OneStringStructure oneStringStructure2;
    ASSERT_TRUE(oneStringStructure1 == oneStringStructure2);

    oneStringStructure1.setOneString(ONE_STRING);
    ASSERT_FALSE(oneStringStructure1 == oneStringStructure2);

    oneStringStructure2.setOneString(ONE_STRING);
    ASSERT_TRUE(oneStringStructure1 == oneStringStructure2);
}

TEST_F(OneStringStructureTest, hashCode)
{
    OneStringStructure oneStringStructure1;
    OneStringStructure oneStringStructure2;
    ASSERT_EQ(oneStringStructure1.hashCode(), oneStringStructure2.hashCode());

    oneStringStructure1.setOneString(ONE_STRING);
    ASSERT_NE(oneStringStructure1.hashCode(), oneStringStructure2.hashCode());

    oneStringStructure2.setOneString(ONE_STRING);
    ASSERT_EQ(oneStringStructure1.hashCode(), oneStringStructure2.hashCode());
}

TEST_F(OneStringStructureTest, read)
{
    zserio::BitStreamWriter writer;
    writeOneStringStructureToByteArray(writer, ONE_STRING);
    size_t writeBufferByteSize;
    const uint8_t* writeBuffer = writer.getWriteBuffer(writeBufferByteSize);
    zserio::BitStreamReader reader(writeBuffer, writeBufferByteSize);

    OneStringStructure oneStringStructure;
    oneStringStructure.read(reader);
    ASSERT_EQ(ONE_STRING, oneStringStructure.getOneString());
}

TEST_F(OneStringStructureTest, write)
{
    OneStringStructure oneStringStructure;
    oneStringStructure.setOneString(ONE_STRING);

    zserio::BitStreamWriter writer;
    oneStringStructure.write(writer);

    size_t writeBufferByteSize;
    const uint8_t* writeBuffer = writer.getWriteBuffer(writeBufferByteSize);
    zserio::BitStreamReader reader(writeBuffer, writeBufferByteSize);
    OneStringStructure readOneStringStructure(reader);
    ASSERT_EQ(ONE_STRING, readOneStringStructure.getOneString());
    ASSERT_TRUE(oneStringStructure == readOneStringStructure);
}

} // namespace one_string_structure
} // namespace structure_types
