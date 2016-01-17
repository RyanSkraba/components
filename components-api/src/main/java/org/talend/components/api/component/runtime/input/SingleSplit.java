package org.talend.components.api.component.runtime.input;

import java.io.DataInput;
import java.io.DataOutput;

/**
 * Created by bchen on 16-1-14.
 */
public class SingleSplit implements Split {
    @Override
    public long getLength() {
        return -1;
    }

    @Override
    public String[] getLocations() {
        return new String[]{""};
    }

    @Override
    public void write(DataOutput var1) {

    }

    @Override
    public void readFields(DataInput var1) {

    }

    @Override
    public int compareTo(Split o) {
        return 0;
    }
}
