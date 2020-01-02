/*
 *  Copyright © 2019 Robin Weiss (http://www.gerdi-project.de/)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package de.gerdiproject.harvest.utils;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;


/**
 * This class writes {@linkplain String} values as empty folders inside a specified folder.
 * These values can be retrieved unordered.
 * This allows to store String values on disk while being considerably faster than
 * an SQLite database.
 *
 * @author Robin Weiss
 */
public class DiskCollection
{
    private final Path parentPath;


    /**
     * Constructor.
     * @param parentFolder the folder in which the empty folders are stored
     */
    public DiskCollection(final File parentFolder)
    {
        this.parentPath = parentFolder.toPath();
    }


    /**
     * Deletes all files inside the cache folder.
     *
     * @throws IOException if the parent folders could not be created
     */
    public void clear() throws IOException
    {
        if (Files.exists(parentPath)) {
            try
                (Stream<Path> dirStream = Files.list(parentPath)) {
                final Iterator<Path> dirIter = dirStream.iterator();

                while (dirIter.hasNext())
                    Files.delete(dirIter.next());

            }
        }

        Files.createDirectories(parentPath);
    }


    /**
     * Adds a {@linkplain String} value to the collection.
     *
     * @param value the value to be added
     *
     * @throws IOException if the value could not be added
     */
    public void add(final String value) throws IOException
    {
        try {
            Files.createDirectory(parentPath.resolve(value));
        } catch (FileAlreadyExistsException ignored) {
        }
    }


    /**
     * Removes and returns a {@linkplain String} value from the collection.
     *
     * @throws IOException if there were problems reading or deleting folders
     *
     * @return any {@linkplain String} value or null, if no value exists.
     */
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public String get() throws IOException
    {
        String nextValue = null;

        try
            (Stream<Path> dirStream = Files.list(parentPath)) {
            final Path firstPath = dirStream.findAny().get();
            Files.delete(firstPath);
            nextValue = firstPath.getFileName().toString();

        } catch (NoSuchElementException ignored) {
            // make the common case fast: retrieve an element from the queue, don't check for every element if it exists
        }

        return nextValue;
    }


    /**
     * Checks if the collection is empty.
     *
     * @throws IOException if there were problems reading files
     *
     * @return true if the collection is empty
     */
    public boolean isEmpty() throws IOException
    {
        boolean hasFiles = false;

        try
            (Stream<Path> dirStream = Files.list(parentPath)) {
            hasFiles = dirStream.findAny().isPresent();
        }

        return !hasFiles;
    }
}
