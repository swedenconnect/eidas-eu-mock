package eu.eidas.auth.commons.attribute.impl.reflect;

import java.io.Serializable;
import java.lang.reflect.TypeVariable;

final class UnresolvedTypeVariableException extends RuntimeException {

    UnresolvedTypeVariableException(TypeVariable<?> typeVariable) {
        super("An exact type is requested, but the type contains a type variable that cannot be resolved.\n" +
                      "   Variable: " + typeVariable.getName() + " from " + typeVariable.getGenericDeclaration() + "\n" +
                      "   Hint: This is usually caused by trying to get an exact type when a generic method who's type parameters are not given is involved.");
    }

}
