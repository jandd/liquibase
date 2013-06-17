package liquibase.diff.compare.core;

import liquibase.database.Database;
import liquibase.diff.ObjectDifferences;
import liquibase.structure.DatabaseObject;
import liquibase.diff.compare.DatabaseObjectComparator;
import liquibase.diff.compare.DatabaseObjectComparatorChain;
import liquibase.structure.core.DataType;

import java.util.HashSet;
import java.util.Set;

public final class DefaultDatabaseObjectComparator implements DatabaseObjectComparator {
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        return PRIORITY_DEFAULT;
    }

    public boolean isSameObject(DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database accordingTo, DatabaseObjectComparatorChain chain) {
        if (databaseObject1.getClass().isAssignableFrom(databaseObject2.getClass()) || databaseObject2.getClass().isAssignableFrom(databaseObject1.getClass())) {
            return nameMatches(databaseObject1, databaseObject2, accordingTo);
        }
        return false;

    }

    public ObjectDifferences findDifferences(DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database accordingTo, DatabaseObjectComparatorChain chain) {

        Set<String> attributes = new HashSet<String>();
        attributes.addAll(databaseObject1.getAttributes());
        attributes.addAll(databaseObject2.getAttributes());

        ObjectDifferences differences = new ObjectDifferences();

        for (String attribute : attributes) {
            Object attribute1 = databaseObject1.getAttribute(attribute, Object.class);
            Object attribute2 = databaseObject2.getAttribute(attribute, Object.class);

            ObjectDifferences.CompareFunction compareFunction;
            if (attribute1 instanceof DatabaseObject || attribute2 instanceof DatabaseObject) {
                Class<? extends DatabaseObject> type;
                if (attribute1 != null) {
                    type = (Class<? extends DatabaseObject>) attribute1.getClass();
                } else {
                    type = (Class<? extends DatabaseObject>) attribute2.getClass();
                }
                compareFunction = new ObjectDifferences.DatabaseObjectNameCompareFunction(type, accordingTo);
            } else if (attribute1 instanceof DataType || attribute2 instanceof DataType) {
                compareFunction = new ObjectDifferences.ToStringCompareFunction();
            } else {
                compareFunction = new ObjectDifferences.StandardCompareFunction();
            }
            differences.compare(attribute, databaseObject1, databaseObject2, compareFunction);

        }

        return differences;
    }

    protected boolean nameMatches(DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database accordingTo) {
        String object1Name = accordingTo.correctObjectName(databaseObject1.getName(), databaseObject1.getClass());
        String object2Name = accordingTo.correctObjectName(databaseObject2.getName(), databaseObject2.getClass());

        if (object1Name == null && object2Name == null) {
            return true;
        }
        if (object1Name == null || object2Name == null) {
            return false;
        }
        if (accordingTo.isCaseSensitive()) {
            return object1Name.equals(object2Name);
        } else {
            return object1Name.equalsIgnoreCase(object2Name);
        }
    }

}