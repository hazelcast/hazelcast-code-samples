package com.hazelcast.samples.querying.domain;

import java.io.Serializable;

import com.hazelcast.core.PartitionAware;

import lombok.Data;

/**
 * <P>
 * This is the key for the "{@code person}" map.
 * </P>
 * <P>
 * This map is <I>partitioned</I>, split into parts with these parts distributed
 * across the available server JVMs. Ordinarily the full key is used to
 * determine which partition contains which <I>key-value</I> pair.
 * </P>
 * <P>
 * Here we use {@link PartitionAware#getPartitionKey()} to override this default
 * routing, and only use part of the key to determine which partition holds the
 * <I>key-value</I> pair.
 * </P>
 * <P>
 * The people "John Doe" and "Jane Doe" are different, they have different keys.
 * But the routing is based only on the surname, so both entries end up in the
 * same partition. See {@code README.md} for why this is an exceptionally bad
 * idea for this particular domain model.
 * </P>
 */
@Data
@SuppressWarnings("serial")
public class PersonKey implements PartitionAware<Byte>, Serializable {

    private String firstName;
    private String lastName;

    /**
     * <P>
     * Routing of keys to partitions is based on the first letter of the last name.
     * Assumes ASCII names.
     * </P>
     *
     * @return 'A' for 'Apple', etc.
     */
    @Override
    public Byte getPartitionKey() {
        return (byte) this.lastName.charAt(0);
    }

}
