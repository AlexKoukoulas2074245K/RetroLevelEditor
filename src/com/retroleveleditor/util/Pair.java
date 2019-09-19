package com.retroleveleditor.util;

public class Pair<T>
{
    public T x, y;

    public Pair(final T x, final T y)
    {
        this.x = x;
        this.y = y;
    }

    public boolean equals(Object other)
    {
        if (other instanceof Pair<?>)
        {
            if ( ((Pair<?>)other).x.equals(this.x) && ((Pair<?>)other).y.equals(this.y))
            {
                return true;
            }
        }
        return false;
    }
}
