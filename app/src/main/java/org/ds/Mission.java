package org.ds;

import java.util.Arrays;
import java.util.Vector;

public class Mission {
    private String strCsv = null;
    private Vector<Entry> vecEntries = new Vector<>();

    public Mission(String strCsv)
    {
        this.setStrCsv(strCsv);
        parseCsv();
    }

    protected void parseCsv()
    {
        if (getStrCsv() != null)
        {
            String lines[] = getStrCsv().split("[\n\r]+");
            for (String line: lines)
            {
                line.trim();
                if (line.length() > 0)
                    getEntries().add(new Entry(line));
            }
        }
    }

    public String getStrCsv() {
        return strCsv;
    }

    public void setStrCsv(String strCsv) {
        this.strCsv = strCsv;
    }

    public Vector<Entry> getEntries() {
        return vecEntries;
    }

    public void setEntries(Vector<Entry> vecEntries) {
        this.vecEntries = vecEntries;
    }

    //single line entry in mission
    class Entry
    {
        private String strCsv = null;
        private String codeword = null;
        private String realLocation = null;
        private String strTime = null; //in hh:mm:ss
        private String teamName = null;
        private Vector<String> vecMembers = new Vector<>();

        public Entry(String strCsv)
        {
            this.setStrCsv(strCsv);
            parseCsv();
        }

        protected void parseCsv()
        {
            if (getStrCsv() != null)
            {
                String lines[] = getStrCsv().split("\\s*,\\s*");
                setCodeword(lines[0]);
                setRealLocation(lines[1]);
                setStrTime(lines[2]);
                setTeamName(lines[3]);
                getMembers().addAll(Arrays.asList(lines[4].split("\\s*;\\s*")));
            }
        }

        public String getStrCsv() {
            return strCsv;
        }

        public void setStrCsv(String strCsv) {
            this.strCsv = strCsv;
        }

        public String getCodeword() {
            return codeword;
        }

        public void setCodeword(String codeword) {
            this.codeword = codeword;
        }

        public String getRealLocation() {
            return realLocation;
        }

        public void setRealLocation(String realLocation) {
            this.realLocation = realLocation;
        }

        public String getStrTime() {
            return strTime;
        }

        public void setStrTime(String strTime) {
            this.strTime = strTime;
        }

        public String getTeamName() {
            return teamName;
        }

        public void setTeamName(String teamName) {
            this.teamName = teamName;
        }

        public Vector<String> getMembers() {
            return vecMembers;
        }

        public void setMembers(Vector<String> vecMembers) {
            this.vecMembers = vecMembers;
        }
    }
}
