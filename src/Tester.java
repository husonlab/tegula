import core.dsymbols.DSymbol;
import core.dsymbols.FDomain;
import core.dsymbols.OrbifoldGroupName;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

/**
 * test code
 * Created by huson on 3/29/16.
 */
public class Tester {

    public static void main(String[] args) throws IOException {
        final DSymbol dsymbol = new DSymbol();

        try (Reader r = new BufferedReader(new FileReader("/Users/huson/cpp/dsymbols/lib/reptiles/in")))
        //Reader r=new StringReader("<1.1:12:2 4 6 8 10 12,12 3 5 7 9 11,8 7 10 9 12 11:6,3 3>");
        {
            while (dsymbol.read(r)) {
                if (dsymbol.getNr1() == 1) {
                    System.err.println(dsymbol);
                    System.err.println("Group: " + OrbifoldGroupName.getGroupName(dsymbol));
                    final FDomain fDomain = new FDomain(dsymbol);
                    System.err.println(fDomain);
                }
            }
        }
    }

}
