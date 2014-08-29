package org.stoev.frizzante;

import org.testng.annotations.Test;

public class OptionTest {
        @Test (expectedExceptions = IllegalArgumentException.class)
	public final void testInvalidOption() {
		GlobalContext<String> g = new GlobalContext.ContextBuilder<String>().grammar("#option NO_SUCH_OPTION").build();
        }
}
