package org.infinispan.protostream.annotations.impl;

import org.infinispan.protostream.ProtobufUtil;
import org.infinispan.protostream.SerializationContext;
import org.infinispan.protostream.annotations.ProtoSchemaBuilder;
import org.infinispan.protostream.annotations.ProtoSchemaBuilderException;
import org.infinispan.protostream.annotations.impl.testdomain.Simple;
import org.infinispan.protostream.annotations.impl.testdomain.TestClass;
import org.infinispan.protostream.annotations.impl.testdomain.TestClass3;
import org.infinispan.protostream.annotations.impl.testdomain.TestEnum;
import org.infinispan.protostream.annotations.impl.testdomain.subpackage.TestClass2;
import org.infinispan.protostream.config.Configuration;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

//todo [anistor] replace IllegalStateException with a real exception and revise all exception message to better indicate the cause (class, field)

//todo [anistor] detect type definition cycles

//todo [anistor] generate debug comments in proto schema (list of initial classes and extra detected classes, source class for each proto definition, source field (+class) and eventual get/set for each proto field)

/**
 * @author anistor@redhat.com
 * @since 3.0
 */
public class ProtoSchemaBuilderTest {

   @org.junit.Rule
   public ExpectedException exception = ExpectedException.none();

   @Test
   public void testNullFileName() throws Exception {
      exception.expect(ProtoSchemaBuilderException.class);
      exception.expectMessage("fileName cannot be null");

      SerializationContext ctx = ProtobufUtil.newSerializationContext(new Configuration.Builder().build());
      ProtoSchemaBuilder protoSchemaBuilder = new ProtoSchemaBuilder();
      protoSchemaBuilder.addClass(Simple.class).build(ctx);
   }

   @Test
   public void testNoAnnotations() throws Exception {
      exception.expect(ProtoSchemaBuilderException.class);
      exception.expectMessage("Class java.lang.Object does not have any @ProtoField annotated fields");

      SerializationContext ctx = ProtobufUtil.newSerializationContext(new Configuration.Builder().build());
      ProtoSchemaBuilder protoSchemaBuilder = new ProtoSchemaBuilder();
      protoSchemaBuilder.fileName("test.proto");
      protoSchemaBuilder.addClass(Object.class).build(ctx);
   }

   @Test
   public void testGeneration() throws Exception {
      SerializationContext ctx = ProtobufUtil.newSerializationContext(new Configuration.Builder().build());
      ProtoSchemaBuilder protoSchemaBuilder = new ProtoSchemaBuilder();
      protoSchemaBuilder
            .fileName("test.proto")
            .packageName("test_package")
            .addClass(TestClass.class)
            .addClass(TestClass3.class)
            .addClass(Simple.class)
            .build(ctx);

      assertTrue(ctx.canMarshall(TestEnum.class));
      assertTrue(ctx.canMarshall(Simple.class));
      assertTrue(ctx.canMarshall(TestClass.class));
      assertTrue(ctx.canMarshall(TestClass.InnerClass.class));
      assertTrue(ctx.canMarshall(TestClass.InnerClass2.class));
      assertTrue(ctx.canMarshall(TestClass2.class));
      assertTrue(ctx.canMarshall(TestClass3.class));

      assertTrue(ctx.canMarshall("test_package.TestEnumABC"));
      assertTrue(ctx.canMarshall("test_package.Simple"));
      assertTrue(ctx.canMarshall("test_package.TestClass2"));
      assertTrue(ctx.canMarshall("test_package.TestClass3"));
      assertTrue(ctx.canMarshall("test_package.TestClass"));
      assertTrue(ctx.canMarshall("test_package.TestClass.InnerClass"));
      assertTrue(ctx.canMarshall("test_package.TestClass.InnerClass2"));

      Simple simple = new Simple();
      simple.afloat = 3.14f;
      byte[] bytes = ProtobufUtil.toWrappedByteArray(ctx, simple);

      Object unmarshalled = ProtobufUtil.fromWrappedByteArray(ctx, bytes);

      assertTrue(unmarshalled instanceof Simple);
      Simple unwrapped = (Simple) unmarshalled;
      assertEquals(3.14f, unwrapped.afloat, 0.001);

      TestClass testClass = new TestClass();
      testClass.surname = "test";
      testClass.testClass2 = new TestClass2();
      testClass.testClass2.address = "test address";
      bytes = ProtobufUtil.toWrappedByteArray(ctx, testClass);

      unmarshalled = ProtobufUtil.fromWrappedByteArray(ctx, bytes);
      assertTrue(unmarshalled instanceof TestClass);
      assertEquals("test", ((TestClass) unmarshalled).surname);
      assertEquals("test address", ((TestClass) unmarshalled).testClass2.address);
   }
}