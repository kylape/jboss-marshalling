/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.test.marshalling;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.jboss.marshalling.ByteInput;
import org.jboss.marshalling.ByteOutput;
import org.jboss.marshalling.Marshaller;
import org.jboss.marshalling.Marshalling;
import org.jboss.marshalling.MarshallingConfiguration;
import org.jboss.marshalling.Unmarshaller;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;

/**
 *
 */
public abstract class TestBase {

    protected final TestMarshallerProvider testMarshallerProvider;
    protected final TestUnmarshallerProvider testUnmarshallerProvider;
    protected final MarshallingConfiguration configuration;

    public static void assertEOF(final ObjectInput objectInput) throws IOException {
        assertTrue("No EOF", objectInput.read() == -1);
    }

    @SuppressWarnings("unchecked")
    private static final Set<Class<?>> nonSameClasses = new HashSet<Class<?>>(Arrays.asList(
            Boolean.class,
            Byte.class,
            Character.class,
            Short.class,
            Integer.class,
            Long.class,
            Float.class,
            Double.class,
            String.class
    ));

    public static void assertEqualsOrSame(final Object a, final Object b) {
        if (a == null || b == null) {
            assertTrue(a == b);
        } else {
            if (nonSameClasses.contains(a.getClass())) {
                assertEquals(a, b);
            } else {
                assertSame(a, b);
            }
        }
    }

    public static void assertEqualsOrSame(final String msg, final Object a, final Object b) {
        if (a == null || b == null) {
            assertTrue(msg, a == b);
        } else {
            if (nonSameClasses.contains(a.getClass())) {
                assertEquals(msg, a, b);
            } else {
                assertSame(msg, a, b);
            }
        }
    }

    @SuppressWarnings({ "ConstructorNotProtectedInAbstractClass" })
    public TestBase(final TestMarshallerProvider testMarshallerProvider, final TestUnmarshallerProvider testUnmarshallerProvider, MarshallingConfiguration configuration) {
        this.testMarshallerProvider = testMarshallerProvider;
        this.testUnmarshallerProvider = testUnmarshallerProvider;
        this.configuration = configuration;
    }

    public void runReadWriteTest(ReadWriteTest readWriteTest) throws Throwable {
        final MarshallingConfiguration readConfiguration = configuration.clone();
        readWriteTest.configureRead(readConfiguration);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream(10240);
        final ByteOutput byteOutput = Marshalling.createByteOutput(baos);

        System.out.println("Read Configuration = " + readConfiguration);
        final Marshaller marshaller = testMarshallerProvider.create(readConfiguration, byteOutput);
        System.out.println("Marshaller = " + marshaller + " (version set to " + readConfiguration.getVersion() + ")");
        readWriteTest.runWrite(marshaller);
        marshaller.finish();
        final byte[] bytes = baos.toByteArray();

        final MarshallingConfiguration writeConfiguration = configuration.clone();
        readWriteTest.configureWrite(writeConfiguration);

        final ByteInput byteInput = Marshalling.createByteInput(new ByteArrayInputStream(bytes));
        System.out.println("Write Configuration = " + writeConfiguration);
        final Unmarshaller unmarshaller = testUnmarshallerProvider.create(writeConfiguration, byteInput);
        System.out.println("Unmarshaller = " + unmarshaller + " (version set to " + writeConfiguration.getVersion() + ")");
        readWriteTest.runRead(unmarshaller);
        unmarshaller.finish();
    }
}
