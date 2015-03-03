package techery.io.snappytest;

/**
 * Created by zen on 3/2/15.
 */
public class Tests {
//    public void insert(int size) {
//        meter.start("Insert " + size);
//
//        for (int i = 0; i < size; i++) {
//            User u = genUser();
//            try {
//                snappyDB.put(String.valueOf(u.id), u);
//            } catch (SnappydbException e) {
//                e.printStackTrace();
//            }
//        }
//
//        meter.finish("Insert " + size);
//    }
//
//    public void insertArray(int size) {
//        meter.start("Insert Array " + size);
//
//        User users[] = new User[size];
//
//        for (int i = 0; i < size; i++) {
//            User u = genUser();
//            users[i] = u;
//        }
//
//        try {
//            snappyDB.put("items", users);
//        } catch (SnappydbException e) {
//            e.printStackTrace();
//        }
//
//        meter.finish("Insert Array " + size);
//    }
//
//    public void read(int size) {
//        for (int i = 0; i < size; i++) {
//            User u = genUser();
//            try {
//                snappyDB.put(String.valueOf(u.id), u);
//            } catch (SnappydbException e) {
//                e.printStackTrace();
//            }
//        }
//
//        meter.start("Read " + size);
//
//        for (int i = 0; i < size; i++) {
//            User u = null;
//            try {
//                u = snappyDB.getObject(String.valueOf(i), User.class);
//            } catch (SnappydbException e) {
//                e.printStackTrace();
//            }
//            u.id = 1;
//        }
//
//        meter.finish("Read " + size);
//    }
//
//    public void readArray(int size) {
//        User users[] = new User[size];
//
//        for (int i = 0; i < size; i++) {
//            User u = genUser();
//            users[i] = u;
//        }
//
//        try {
//            snappyDB.put("items", users);
//        } catch (SnappydbException e) {
//            e.printStackTrace();
//        }
//
//
//        meter.start("Read array " + size);
//
//        User u[] = new User[0];
//        try {
//            u = snappyDB.getObjectArray("items", User.class);
//        } catch (SnappydbException e) {
//            e.printStackTrace();
//        }
//        assert u.length == size;
//
//        meter.finish("Read array " + size);
//    }
//
//    public void findKeys(int size) {
//        for (int i = 0; i < size; i++) {
//            User u = genUser();
//            try {
//                snappyDB.put("key:" + String.valueOf(u.id), u);
//            } catch (SnappydbException e) {
//                e.printStackTrace();
//            }
//        }
//
//        meter.start("Find keys " + size);
//
//        try {
//            String[] keys = snappyDB.findKeys("key");
//            assert keys.length > size;
//        } catch (SnappydbException e) {
//            e.printStackTrace();
//        }
//
//        meter.finish("Find keys " + size);
//    }
//
//    private void setup() {
//        try {
//            if (snappyDB != null) {
//                snappyDB.destroy();
//            }
//        } catch (SnappydbException e) {
//            e.printStackTrace();
//        }
//
//        try {
//            snappyDB = DBFactory.open(this);
//        } catch (SnappydbException e) {
//            e.printStackTrace();
//        }
//
//        lastId = 0;
//    }
//
//    public interface Body {
//        void run(int size);
//    }
//
//    public void run(final int size, final Body body) {
//        executorService.submit(new Runnable() {
//            @Override
//            public void run() {
//                setup();
//
//                body.run(size);
//
//                Log.d("Memory usage", getUsedMemorySize() / (1024 * 1024) + " mega bytes");
//            }
//        });
//    }
//
//    public void runAll(final int sizes[], final Body body) {
//        for (int size : sizes) {
//            run(size, body);
//        }
//    }



//        int[] sizes = {1, 5, 10, 100, 500, 1000, 10000, 100000, 1000000};
//
//        runAll(sizes, new Body() {
//            @Override
//            public void run(int size) {
//                findKeys(size);
//            }
//        });
//
//        runAll(sizes, new Body() {
//            @Override
//            public void run(int size) {
//                insert(size);
//            }
//        });
//
//        runAll(sizes, new Body() {
//            @Override
//            public void run(int size) {
//                insertArray(size);
//            }
//        });
//
//        runAll(sizes, new Body() {
//            @Override
//            public void run(int size) {
//                read(size);
//            }
//        });
//
//        runAll(sizes, new Body() {
//            @Override
//            public void run(int size) {
//                readArray(size);
//            }
//        });
//
//        runAll(sizes, new Body() {
//            @Override
//            public void run(int size) {
//                readArray(size);
//            }
//        });
}
