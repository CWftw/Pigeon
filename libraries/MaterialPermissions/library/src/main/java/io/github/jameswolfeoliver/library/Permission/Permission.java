package io.github.jameswolfeoliver.library.Permission;

public class Permission {

    private String simpleName;
    private String systemName;
    private String rational;
    private int iconResourceId;
    private int backgroundColorResourceId;

    private Permission() { }

    public int getBackgroundColorResourceId() {
        return backgroundColorResourceId;
    }

    public int getIconResourceId() {
        return iconResourceId;
    }

    public String getRational() {
        return rational;
    }

    public String getSimpleName() {
        return simpleName;
    }

    public String getSystemName() {
        return systemName;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Permission && super.equals(obj) && this.systemName.equals(((Permission) obj).systemName);
    }

    public static class Builder {
        Permission permission;

        public Builder() {
            permission = new Permission();
            this.permission.iconResourceId = 0;
            this.permission.rational = "";
            this.permission.simpleName = "";
            this.permission.systemName = "";
            this.permission.backgroundColorResourceId = 0;
        }

        public Permission build() {
            return this.permission;
        }

        public Builder setIconResourceId(int iconResourceId) {
            this.permission.iconResourceId = iconResourceId;
            return this;
        }

        public Builder setRational(String rational) {
            this.permission.rational = rational;
            return this;
        }

        public Builder setSimpleName(String simpleName) {
            this.permission.simpleName = simpleName;
            return this;
        }

        public Builder setSystemName(String systemName) {
            this.permission.systemName = systemName;
            return this;
        }

        public Builder setBackgroundColorResourceId(int backgroundColorResourceId) {
            this.permission.backgroundColorResourceId = backgroundColorResourceId;
            return this;
        }
    }
}
