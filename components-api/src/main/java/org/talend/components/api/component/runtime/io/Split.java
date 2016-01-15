package org.talend.components.api.component.runtime.io;

import java.io.DataInput;
import java.io.DataOutput;

/**
 * Created by bchen on 16-1-13.
 */

public interface Split {
    long getLength();

    String[] getLocations();

    void write(DataOutput var1);

    void readFields(DataInput var1);

    public int compareTo(Split o);
}
