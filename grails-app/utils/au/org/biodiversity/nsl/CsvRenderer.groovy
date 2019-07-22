package au.org.biodiversity.nsl

/**
 * User: pmcneil
 * Date: 6/07/16
 *
 */
class CsvRenderer {

    /**
     * Render a list of list values as CSV.
     *
     * All values are treated as strings
     *
     * @param headers
     * @param values
     * @return
     */
    public static String renderAsCsv(List<String> headers, List<List> values) {
        StringBuilder out = StringBuilder.newInstance()
        out << '"' + headers.join('","') + '"\n'
        out << values.collect{List val ->
            '"' + val.join('","') + '"'
        }.join('\n')
        return out.toString()
    }

}
