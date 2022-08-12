package ru.miit.contentuploader;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.miit.contentuploader.utils.NaturalSortComparator;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HumanSortTest {

    @Test
    public void testSorting() {
        List<String> names = Stream.of(1, 10, 100, 2, 20, 3, 30, 9)
                .map(num -> String.format("something %d.jpg", num))
                .collect(Collectors.toList());
        names.add(0, "zzz");
        names.add(names.size() - 1, "aaa");


        List<String> expected = Stream.of(1, 2, 3, 9, 10, 20, 30, 100)
                .map(num -> String.format("something %d.jpg", num))
                .collect(Collectors.toList());
        expected.add(0, "aaa");
        expected.add(names.size() - 1, "zzz");


        names.sort(new NaturalSortComparator());

//        System.out.println(names);
        Assertions.assertThat(names)
                .containsExactly(expected.toArray(new String[0]));
    }

}
