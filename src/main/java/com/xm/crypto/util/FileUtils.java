package com.xm.crypto.util;

import com.xm.crypto.entity.Crypto;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Utility class
 */
public final class FileUtils {

    private static final String FILE_NAME_TEMPLATE = "_values.csv";

    private static final CsvSchema CSV_SCHEMA;
    private static final CsvMapper CSV_MAPPER;

    static {
        CSV_MAPPER = new CsvMapper();
        CSV_SCHEMA = CSV_MAPPER.schemaFor(Crypto.class).withSkipFirstDataRow(true);
    }

    private FileUtils() {}

    /**
     * Returns MappingIterator for the provided file
     * @param file csv file containing crypto data
     * @return MappingIterator<Crypto>
     * @throws IOException can be thrown by readValues(file)
     */
    public static MappingIterator<Crypto> getMappingIterator(final File file) throws IOException {
        return CSV_MAPPER.readerFor(Crypto.class).with(CSV_SCHEMA).readValues(file);
    }

    /**
     * Returns a list of files in the provided folder
     * @param folder folder containing csv files
     * @return List<File>
     */
    public static List<File> listFilesForFolder(final File folder) {
        return Arrays.stream(Objects.requireNonNull(folder.listFiles()))
                .filter(f -> f.getName().contains(FILE_NAME_TEMPLATE))
                .collect(Collectors.toUnmodifiableList());
    }
}
