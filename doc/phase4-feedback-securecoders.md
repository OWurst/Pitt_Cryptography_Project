# CS 1653 Project: Phase 4 Feedback

__Group:__ secureCoders

__Names:__ Abramowitz, Gavin M; Bartlett, Michael; Charlesworth, Luke J; Wurst, Owen

__Users:__ gma42; mab650; ljc54; omw3

## Comments

### Documentation and writeup

It looks like you're using the same key for the MAC and the encryption, when
general best practices say to use different keys for different purposes. (For
instance, derive two keys from the Diffie-Hellman secret using hashing and two
different constants. See SSH for an example.)

I see no reason for RS IDs to be random, which i mentioned during review. Why
not just use the whole hash of the public key? Adding randomness (and using only
a subset of the key material) just seems to add more possibility of collisions.
Discord comments suggested that this should have been simplified, but it didn't
make it to the writeup.

Otherwise, the solutions look good.

45 / 50

### Design approval


10 / 10

### Demo and code


35 / 35

### Scheduling demo


5 / 5

### Other notes



## Overall grade

95 / 100

