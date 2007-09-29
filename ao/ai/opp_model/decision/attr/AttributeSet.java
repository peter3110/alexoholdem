package ao.ai.opp_model.decision.attr;

import java.util.Collection;

/**
 *
 */
public interface AttributeSet<T>
{
    public boolean isDescrete();

    public Object  type();
    public boolean typeEquals(AttributeSet<?> attributeSet);

    public Attribute<T> instanceOf(T value);

    public Collection<? extends Attribute<T>> values();
}
