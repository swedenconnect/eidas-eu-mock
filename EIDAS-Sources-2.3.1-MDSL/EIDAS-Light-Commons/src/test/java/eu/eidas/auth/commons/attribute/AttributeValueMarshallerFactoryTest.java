/* 
#   Copyright (c) 2017 European Commission  
#   Licensed under the EUPL, Version 1.2 or – as soon they will be 
#   approved by the European Commission - subsequent versions of the 
#    EUPL (the "Licence"); 
#    You may not use this work except in compliance with the Licence. 
#    You may obtain a copy of the Licence at: 
#    * https://joinup.ec.europa.eu/page/eupl-text-11-12  
#    *
#    Unless required by applicable law or agreed to in writing, software 
#    distributed under the Licence is distributed on an "AS IS" basis, 
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
#    See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package eu.eidas.auth.commons.attribute;

import java.math.BigInteger;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * AttributeValueMarshallerFactoryTest
 *
 * @since 1.1
 */
public final class AttributeValueMarshallerFactoryTest {

    interface AIntf<T> {

    }

    static class A implements AIntf<String> {

    }

    static class B extends A {

    }

    static class B2 extends B {

    }

    interface CIntf<T> extends AIntf<T> {

    }

    interface DIntf extends CIntf<Boolean> {

    }

    static class D implements DIntf {

    }

    static class E<T> implements AIntf<T> {

    }

    static class F extends E<Integer> {

    }

    static class G<K, V> extends E<V> {

    }

    static class H extends G<String, Long> {

    }

    interface JIntf<K, V> extends CIntf<V> {

    }

    static class L implements JIntf<Character, Short> {

    }

    static class M extends L {

    }

    static class N extends M {

    }

    interface OIntf<T> {

    }

    static class P implements OIntf<Double>, AIntf<CharSequence> {

    }

    static class Q<T> {

    }

    interface R extends OIntf<Double>, AIntf<CharSequence> {

    }


    static class S extends Q<Byte> implements R {

    }

    static class U<T2,T3,T1> extends Q<T1> implements OIntf<T2>, CIntf<T3> {

    }

    static class W extends U<Runnable, BigInteger, Package> {

    }

    @Test
    public void findParameterizedTypeFromClassImplementingParameterizedInterface() throws Exception {

        Class<?> parameterizedType = AttributeValueMarshallerFactory.findParameterizedType(A.class, AIntf.class);
        assertEquals(String.class, parameterizedType);

    }

    @Test
    public void findParameterizedTypeFromSubClassImplementingParameterizedInterface() throws Exception {

        Class<?> parameterizedType = AttributeValueMarshallerFactory.findParameterizedType(B.class, AIntf.class);
        assertEquals(String.class, parameterizedType);

        parameterizedType = AttributeValueMarshallerFactory.findParameterizedType(B2.class, AIntf.class);
        assertEquals(String.class, parameterizedType);

    }

    @Test
    public void findParameterizedTypeFromSubSubClassImplementingParameterizedInterface() throws Exception {

        Class<?> parameterizedType = AttributeValueMarshallerFactory.findParameterizedType(D.class, AIntf.class);
        assertEquals(Boolean.class, parameterizedType);

    }

    @Test
    public void findParameterizedTypeFromClassImplementingParameterizedClass() throws Exception {

        Class<?> parameterizedType = AttributeValueMarshallerFactory.findParameterizedType(F.class, AIntf.class);
        assertEquals(Integer.class, parameterizedType);

    }

    @Test
    public void findParameterizedTypeFromClassImplementingMultiParameterizedClass() throws Exception {

        Class<?> parameterizedType = AttributeValueMarshallerFactory.findParameterizedType(H.class, AIntf.class);
        assertEquals(Long.class, parameterizedType);

    }

    @Test
    public void findParameterizedTypeFromClassImplementingMultiParameterizedInterface() throws Exception {

        Class<?> parameterizedType = AttributeValueMarshallerFactory.findParameterizedType(L.class, AIntf.class);
        assertEquals(Short.class, parameterizedType);
    }

    @Test
    public void findParameterizedTypeFromSubClassImplementingMultiParameterizedInterface() throws Exception {

        Class<?> parameterizedType = AttributeValueMarshallerFactory.findParameterizedType(N.class, AIntf.class);
        assertEquals(Short.class, parameterizedType);
    }

    @Test
    public void findParameterizedTypeFromClassImplementingParameterizedMultiInterfaces() throws Exception {

        Class<?> parameterizedType = AttributeValueMarshallerFactory.findParameterizedType(P.class, AIntf.class);
        assertEquals(CharSequence.class, parameterizedType);
    }

    @Test
    public void findParameterizedTypeFromSubClassImplementingParameterizedMultiInterfaces() throws Exception {

        Class<?> parameterizedType = AttributeValueMarshallerFactory.findParameterizedType(S.class, AIntf.class);
        assertEquals(CharSequence.class, parameterizedType);
    }

    @Test
    public void findParameterizedTypeFromSubClassImplementingParameterizedClassAndInterfaces() throws Exception {

        Class<?> parameterizedType = AttributeValueMarshallerFactory.findParameterizedType(W.class, AIntf.class);
        assertEquals(BigInteger.class, parameterizedType);
    }
}
