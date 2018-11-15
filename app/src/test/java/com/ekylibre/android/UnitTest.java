package com.ekylibre.android;

import com.ekylibre.android.utils.PhytosanitaryMiscibility;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
class UnitTest {

    @Test
    void phytoMix_isCorrect() throws Exception {

        HashMap<List<Integer>, Boolean> map = new HashMap<>();

        map.put(Arrays.asList(5,5), false);
        map.put(Arrays.asList(5,4), false);
        map.put(Arrays.asList(5,3), false);
        map.put(Arrays.asList(5,2), false);
        map.put(Arrays.asList(5,1), false);

        map.put(Arrays.asList(4,5), false);
        map.put(Arrays.asList(4,4), false);
        map.put(Arrays.asList(4,3), true);
        map.put(Arrays.asList(4,2), true);
        map.put(Arrays.asList(4,1), true);

        map.put(Arrays.asList(3,5), false);
        map.put(Arrays.asList(3,4), true);
        map.put(Arrays.asList(3,3), false);
        map.put(Arrays.asList(3,2), true);
        map.put(Arrays.asList(3,1), true);

        map.put(Arrays.asList(2,5), false);
        map.put(Arrays.asList(2,4), true);
        map.put(Arrays.asList(2,3), true);
        map.put(Arrays.asList(2,2), false);
        map.put(Arrays.asList(2,1), true);

        map.put(Arrays.asList(1,5), false);
        map.put(Arrays.asList(1,4), true);
        map.put(Arrays.asList(1,3), true);
        map.put(Arrays.asList(1,2), true);
        map.put(Arrays.asList(1,1), true);

        for (Map.Entry<List<Integer>, Boolean> entry : map.entrySet()) {
            assertEquals(entry.getValue(), PhytosanitaryMiscibility.mixIsAuthorized(entry.getKey()));
        }
    }
}