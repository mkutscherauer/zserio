#include "gtest/gtest.h"

#include "templates/instantiate_type_as_choice_field/InstantiateTypeAsChoiceField.h"

namespace templates
{
namespace instantiate_type_as_choice_field
{

TEST(InstantiateTypeAsChoiceFieldTest, readWrite)
{
    InstantiateTypeAsChoiceField instantiateTypeAsChoiceField;
    instantiateTypeAsChoiceField.initialize(true);
    instantiateTypeAsChoiceField.setTest(Test32{13});

    zserio::BitStreamWriter writer;
    instantiateTypeAsChoiceField.write(writer);
    size_t bufferSize = 0;
    const uint8_t* buffer = writer.getWriteBuffer(bufferSize);

    zserio::BitStreamReader reader(buffer, bufferSize);
    InstantiateTypeAsChoiceField readInstantiateTypeAsChoiceField(reader, true);

    ASSERT_TRUE(instantiateTypeAsChoiceField == readInstantiateTypeAsChoiceField);
}

} // namespace instantiate_type_as_choice_field
} // namespace templates
