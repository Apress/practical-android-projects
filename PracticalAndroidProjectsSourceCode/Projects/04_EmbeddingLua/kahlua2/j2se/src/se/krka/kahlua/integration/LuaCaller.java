/*
 Copyright (c) 2009 Kristofer Karlsson <kristofer.karlsson@gmail.com>, Per Malmén <per.malmen@gmail.com>

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 */

package se.krka.kahlua.integration;

import se.krka.kahlua.converter.KahluaConverterManager;
import se.krka.kahlua.vm.KahluaThread;

public class LuaCaller {
	
	private final KahluaConverterManager converterManager;

	public LuaCaller(KahluaConverterManager converterManager) {
		this.converterManager = converterManager;
	}

	public Object[] pcall(KahluaThread thread, Object functionObject, Object... args) {
		if (args != null) {
			for (int i = args.length - 1; i >= 0; i--) {
				args[i] = converterManager.fromJavaToLua(args[i]);
			}
		}
		Object[] results = thread.pcall(functionObject, args);
		return results;
	}

	public LuaReturn protectedCall(KahluaThread thread, Object functionObject, Object... args) {
		return LuaReturn.createReturn(pcall(thread, functionObject, args));
	}
}
