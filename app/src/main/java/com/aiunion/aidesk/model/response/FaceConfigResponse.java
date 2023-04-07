package com.aiunion.aidesk.model.response;

import java.util.List;

public class FaceConfigResponse {
        private int page;
        private int total;
        private List<ConfigResult> result;

    public FaceConfigResponse(int page, int total, List<ConfigResult> result) {
        this.page = page;
        this.total = total;
        this.result = result;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<ConfigResult> getResult() {
        return result;
    }

    public void setResult(List<ConfigResult> result) {
        this.result = result;
    }

    public class ConfigResult {
        private int id;
        private String var;
        private String val;
        private String type;
        private String name;
        private String memo;

        // constructors, getters, and setters

        public ConfigResult(int id, String var, String val, String type, String name, String memo) {
            this.id = id;
            this.var = var;
            this.val = val;
            this.type = type;
            this.name = name;
            this.memo = memo;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getVar() {
            return var;
        }

        public void setVar(String var) {
            this.var = var;
        }

        public String getVal() {
            return val;
        }

        public void setVal(String val) {
            this.val = val;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getMemo() {
            return memo;
        }

        public void setMemo(String memo) {
            this.memo = memo;
        }
    }

}


