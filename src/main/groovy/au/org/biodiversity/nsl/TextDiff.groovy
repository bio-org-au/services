package au.org.biodiversity.nsl

/**
 * User: pmcneil
 * Date: 12/07/18
 *
 */
class TextDiff {

    private static int LOOK_DOWN = 1
    private static int LOOK_RIGHT = 2
    private static int SKIP = 3

    /**
     * chunks represents a list of chunks of text from a that are in (common) b
     * chunks contains all of string b only
     */
    List<Chunk> chunks = []
    String a
    String b

    TextDiff(String a, String b) {
        this.a = a
        this.b = b
        if (sanityCheck(a, b)) {
            wordDiff(a, b)
        }
    }

    String diffHtml(String tag, String cssClass) {
        htmlMarkup(chunks, tag, cssClass)
    }

    private Boolean sanityCheck(String a, String b) {
        if (a && !b) {
            chunks.add(new Chunk(text: '', common: false))
            return false
        }
        if (b && !a) {
            chunks.add(new Chunk(text: b, common: false))
            return false
        }
        if (!a && !b) {
            chunks.add(new Chunk(text: '', common: false))
            return false
        }
        return true
    }

    void wordDiff(String a, String b) {
        List<String> wA = splitWords(a)
        List<String> wB = splitWords(b)

        chunkMatrix(xyMatrix(wA, wB), wB, false)
    }

    static List<String> splitWords(String source) {
        List<String> words = []
        StringBuilder word = new StringBuilder()
        for (ch in source) {
            if (ch =~ /[a-zA-Z0-9]/) {
                word.append(ch)
            } else {
                if (word.length() > 0) {
                    words.add(word.toString())
                    word = new StringBuilder()
                }
                words.add(ch)
            }
        }
        if (word.length() > 0) {
            words.add(word.toString())
        }
        return words
    }

    private chunkMatrix(int[][] xy, List<String> b, Boolean word) {
        Boolean diagonalMove = false
        int maxX = xy.size()
        int maxY = xy[0].size()
        int x = 0
        int y = 0

        while (x < maxX && y < maxY) {
            boolean lastY = y == maxY - 1
            if (xy[x][y] > 0) {
                if (diagonalMove || lastY || (xy[x][y + 1] == 0)) {
                    addCommonChunk(b[y], word)
                    diagonalMove = true
                    x++
                    y++
                } else {
                    addMissingChunk(b[y], word)
                    y++
                    diagonalMove = false
                }
            } else {
                int whichWay = lookAhead(xy, x, y, maxX, maxY)
                switch (whichWay) {
                    case LOOK_DOWN:
                        addMissingChunk(b[y], word)
                        y++
                        break
                    case LOOK_RIGHT:
                        addBlankMissingChunk()
                        x++
                        break
                    case SKIP:
                        addMissingChunk(b[y], word)
                        x++
                        y++
                }
                diagonalMove = false
            }
        }
        if (y < maxY) {
            addMissingRemainder(y, b, word)
        }
    }

    private static int lookAhead(int[][] xy, int x, int y, int maxX, int maxY) {
        int i = x
        int j = y
        boolean right = false
        boolean down = false
        while (i < maxX) {
            if (xy[i++][j] > 0) {
                right = true
                break
            }
        }
        i = x
        while (j < maxY) {
            if (xy[i][j++] > 0) {
                down = true
                break
            }
        }
        if (maxX > maxY && right)
            return LOOK_RIGHT
        return down ? LOOK_DOWN : SKIP
    }

    private static int[][] xyMatrix(List<String> a, List<String> b) {
        int maxX = a.size()
        int maxY = b.size()
        int[][] xy = new int[maxX][maxY]
        int x = 0

        while (x < maxX) {
            int y = 0
            while (y < maxY) {
                xy[x][y] = a[x] == b[y] ? 1 : 0
                y++
            }
            x++
        }
        return xy
    }

    private static printMatrix(int[][] xy) {
        int maxX = xy.size()
        int maxY = xy[0].size()
        int y = 0
        while (y < maxY) {
            int x = 0
            while (x < maxX) {
                print " ${xy[x][y]}"
                x++
            }
            println ''
            y++
        }
    }

    private static String htmlMarkup(List<Chunk> chunks, String tag, String cssClass) {
        String result = ''
        String open = "<$tag class=\"$cssClass\">"
        String close = "</$tag>"
        for (chunk in chunks) {
            result += chunk.common ? chunk.text : "${open}${chunk.text}${close}"
        }
        return result
    }

    private addMissingRemainder(int y, List<String> b, Boolean word) {
        addMissingChunk(b[y..-1].join(''), word)
    }

    private addCommonChunk(String text, Boolean word) {
        String t = (word ? ' ' + text : encode(text))
        Chunk lastChunk = chunks.size() ? chunks.last() : null
        if (lastChunk && lastChunk.common) {
            lastChunk.text += t
        } else {
            chunks.add(new Chunk(text: t, common: true))
        }
    }

    private addMissingChunk(String text, Boolean word) {
        String t = (word ? ' ' + text : encode(text))
        Chunk lastChunk = chunks.size() ? chunks.last() : null
        if (lastChunk && !lastChunk.common) {
            lastChunk.text += t
        } else {
            chunks.add(new Chunk(text: t, common: false))
        }
    }

    private addBlankMissingChunk() {
        Chunk lastChunk = chunks.size() ? chunks.last() : null
        if (lastChunk && !lastChunk.common) {
            if (!lastChunk.text.endsWith(' ')) {
                lastChunk.text += ' '
            }
        } else {
            chunks.add(new Chunk(text: ' ', common: false))
        }
    }

    private static String encode(String s) {
        if (s.startsWith('&') && s.endsWith(';')) {
            return s
        }
        s.replaceAll(/&/, '&amp;')
         .replaceAll(/</, '&lt;')
         .replaceAll(/>/, '&gt;')

    }

}