package it.polito.ai.mmap.esercitazione2.test.mongo;

import it.polito.ai.mmap.esercitazione2.bean.User;
import it.polito.ai.mmap.esercitazione2.repository.UserMongoRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserMongoRepositoryTest {

    @Autowired
    private UserMongoRepository userMongoRepository;

    @Before
    public void setUp() throws Exception {

        User user1 = new User("Alice", 23);
        User user2 = new User("Bob", 38);
        // id = null before save
        assertNull(user1.getId());
        assertNull(user2.getId());
        // save and check id
        userMongoRepository.save(user1);
        userMongoRepository.save(user2);
        System.out.println(user1+" and "+user2+" saved");
        assertNotNull(user1.getId());
        assertNotNull(user2.getId());

    }

    @Test
    public void testFetchData() {

        // Test data retrieval
        User userA = userMongoRepository.findByName("Bob");
        assertNotNull(userA);
        assertEquals(38, userA.getAge());

        // Get all users, expected 2
        Iterable<User> users = userMongoRepository.findAll();
        int count = 0;
        for(User user: users)
            count++;
        assertEquals(2,count);


    }

    @Test
    public void testDataUpdate() {

        // Test update
        User userB = userMongoRepository.findByName("Alice");
        userB.setAge(32);
        userMongoRepository.save(userB);
        User userC = userMongoRepository.findByName("Alice");
        assertNotNull(userC);
        assertEquals(32,userC.getAge());

    }

    @After
    public void tearDown() throws Exception {
        userMongoRepository.deleteAll();
    }

}
