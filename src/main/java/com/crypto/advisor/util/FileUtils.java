package com.crypto.advisor.util;

import java.util.List;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;

import com.crypto.advisor.entity.Crypto;

// TODO: inject CSV mapper as a Spring bean into a service and move all specific to the business logic there

/**
 * Utility class for interacting with files
 */
public final class FileUtils {

    private static final ObjectReader OBJECT_READER;

    static {

        var mapper = new CsvMapper();

        var schema = mapper.schemaFor(Crypto.class)
            .withSkipFirstDataRow(true);

        OBJECT_READER = mapper.readerFor(Crypto.class).with(schema);
    }

    private FileUtils() {}

    /**
     * Returns MappingIterator for the provided file
     * @param file csv file containing crypto data
     * @return MappingIterator<Crypto>
     * @throws IOException can be thrown by readValues(file)
     */
    public static MappingIterator<Crypto> getMappingIterator(File file) throws IOException {
        return OBJECT_READER.readValues(file);
    }

    /**
     * Returns a list of files in the provided folder
     * @param folder folder containing csv files
     * @return List<File>
     */
    public static List<File> listFilesForFolder(File folder) {
        return Arrays.stream(Objects.requireNonNull(folder.listFiles()))
                .filter(f -> f.getName().contains("_values.csv"))
                .collect(Collectors.toUnmodifiableList());
    }
}