/*
 * $#
 * FOS Weka
 *  
 * Copyright (C) 2013 Feedzai SA
 *  
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #$
 */
package com.feedzai.fos.impl.weka.utils;

import com.feedzai.fos.api.FOSException;
import com.feedzai.fos.api.ModelDescriptor;
import com.feedzai.fos.common.validation.NotNull;
import com.feedzai.fos.impl.weka.utils.pmml.PMMLConsumer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SerializationUtils;

import java.io.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Deep clonner to copy objects via serialization (thead safe!)
 *
 * @author Marco Jorge (marco.jorge@feedzai.com)
 */
public class Cloner<T extends Serializable> {
    private byte[] serializedObject;

    /**
     * Creates a new clonner from the given serialized object.
     *
     * @param serializedObject the serialized object that will cloned
     */
    public Cloner(byte[] serializedObject) {
        checkNotNull(serializedObject, "The serialized object cannot be null");

        this.serializedObject = serializedObject;
    }


    /**
     * Creates a clonner from the given object.
     *
     * @param serializable the object to clone
     * @throws IOException when there were problems serializing the object
     */
    public Cloner(T serializable) throws IOException {
        checkNotNull(serializable, "The serialized object cannot be null");

        ObjectOutputStream oos = null;
        ByteArrayOutputStream baos = null;

        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(serializable);
            oos.flush();

            this.serializedObject = baos.toByteArray();
        } finally {
            IOUtils.closeQuietly(baos);
            IOUtils.closeQuietly(oos);
        }
    }

    /**
     * Creates a clonner by reading a serialized object from file.
     *
     * @param descriptor A {@link com.feedzai.fos.api.ModelDescriptor} with the information about the classifier.
     * @throws IOException when there were problems reading the file
     */
    public Cloner(ModelDescriptor descriptor) throws IOException {
        checkNotNull(descriptor.getModelFilePath(), "Source file cannot be null");

        File file = new File(descriptor.getModelFilePath());

        checkArgument(file.exists(), "Source file '"+ file.getAbsolutePath() + "' must exist");

        switch (descriptor.getFormat()) {
            case BINARY:
                this.serializedObject = FileUtils.readFileToByteArray(file);
                break;
            case PMML:
                try {
                    this.serializedObject = SerializationUtils.serialize(PMMLConsumer.consume(file));
                } catch (FOSException e) {
                    throw new RuntimeException("Failed to consume PMML file.", e);
                }
                break;
        }
    }

    /**
     * Gets a fresh clone of the object.
     *
     * @return a fresh clone
     * @throws IOException            when there were problems serializing the object
     * @throws ClassNotFoundException when the serialized objects's class was not found
     */
    @NotNull
    public T get() throws IOException, ClassNotFoundException {
        /* cannot be pre-instantiated to enable thread concurrency*/
        ByteArrayInputStream byteArrayInputStream = null;
        ObjectInputStream objectInputStream = null;
        try {
            byteArrayInputStream = new ByteArrayInputStream(this.serializedObject);
            objectInputStream = new ObjectInputStream(byteArrayInputStream);

            return (T) objectInputStream.readObject();
        } finally {
            IOUtils.closeQuietly(byteArrayInputStream);
            IOUtils.closeQuietly(objectInputStream);
        }
    }

    /**
     * Returns a copy of the serialized object.
     *
     * @return a copy (this object is immutable)
     */
    @NotNull
    public byte[] getSerialized() {
        byte[] result = new byte[serializedObject.length];
        System.arraycopy(serializedObject, 0, result, 0, serializedObject.length);

        return result;
    }

    /**
     * Writes the serialized object to file.
     *
     * @param file the file to write to (will be overwritten)
     * @throws IOException when the file could not be written
     */
    public void write(File file) throws IOException {
        checkNotNull(file, "Output file cannot be null");

        FileUtils.writeByteArrayToFile(file, serializedObject);
    }
}
