/*
 Copyright (c) 2009 Kristofer Karlsson <kristofer.karlsson@gmail.com>

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

package se.krka.kahlua.threading;

import se.krka.kahlua.vm.KahluaTable;
import se.krka.kahlua.vm.KahluaThread;
import se.krka.kahlua.vm.Platform;

import java.io.PrintStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A specialized KahluaThread that verifies that a KahluaThread is not used by multiple threads.
 */
public class VerifiedSingleKahluaThread extends KahluaThread {
    private final Lock lock = new ReentrantLock();

    public VerifiedSingleKahluaThread(Platform platform, KahluaTable environment) {
        super(platform, environment);
    }

    public VerifiedSingleKahluaThread(PrintStream stream, Platform platform, KahluaTable environment) {
        super(stream, platform, environment);
    }

    private void lock() {
        if (!lock.tryLock()) {
            throw new IllegalStateException("Multiple threads may not access the same lua thread");
        }
    }

    private void unlock() {
        lock.unlock();
    }

    @Override
    public int call(int arguments) {
        lock();
        try {
            return super.call(arguments);
        } finally {
            unlock();
        }
    }

    @Override
    public int pcall(int arguments) {
        lock();
        try {
            return super.pcall(arguments);
        } finally {
            unlock();
        }
    }

    @Override
    public Object[] pcall(Object fun) {
        lock();
        try {
            return super.pcall(fun);
        } finally {
            unlock();
        }
    }

    @Override
    public final Object[] pcall(Object fun, Object[] args) {
        lock();
        try {
            return super.pcall(fun, args);
        } finally {
            unlock();
        }
    }

    @Override
    public void setmetatable(Object o, KahluaTable metatable) {
        lock();
        try {
            super.setmetatable(o, metatable);
        } finally {
            unlock();
        }
    }

    @Override
    public Object call(Object fun, Object arg1, Object arg2, Object arg3) {
        lock();
        try {
            return super.call(fun, arg1, arg2, arg3);
        } finally {
            unlock();
        }
    }

    @Override
    public Object call(Object fun, Object[] args) {
        lock();
        try {
            return super.call(fun, args);
        } finally {
            unlock();
        }
    }

    @Override
    public KahluaTable getEnvironment() {
        lock();
        try {
            return super.getEnvironment();
        } finally {
            unlock();
        }
    }

    @Override
    public Object getMetaOp(Object o, String meta_op) {
        lock();
        try {
            return super.getMetaOp(o, meta_op);
        } finally {
            unlock();
        }
    }

    @Override
    public Object getmetatable(Object o, boolean raw) {
        lock();
        try {
            return super.getmetatable(o, raw);
        } finally {
            unlock();
        }
    }

    @Override
    public Object tableGet(Object table, Object key) {
        lock();
        try {
            return super.tableGet(table, key);
        } finally {
            unlock();
        }
    }

    @Override
    public void tableSet(Object table, Object key, Object value) {
        lock();
        try {
            super.tableSet(table, key, value);
        } finally {
            unlock();
        }
    }
}