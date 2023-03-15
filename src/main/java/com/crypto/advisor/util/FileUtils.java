package com.crypto.advisor.util;

import java.util.List;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.io.File;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FileUtils {

    public static List<File> listFilesInFolderByPattern(File folder, String pattern) {
        return Arrays.stream(Objects.requireNonNull(folder.listFiles()))
                .filter(f -> f.getName().contains(pattern))
                .collect(Collectors.toUnmodifiableList());
    }
}