// MongoDB initialization script for Procar backend

// Switch to admin database to create users
db = db.getSiblingDB('admin');

// Create admin user (if not exists)
if (!db.getUser('admin')) {
    db.createUser({
        user: 'admin',
        pwd: 'password',
        roles: [
            { role: 'userAdminAnyDatabase', db: 'admin' },
            { role: 'readWriteAnyDatabase', db: 'admin' }
        ]
    });
}

// Switch to application databases
db = db.getSiblingDB('procar-gateway');
db.createCollection('health_checks');

db = db.getSiblingDB('procar-auth');
db.createCollection('users');
db.createCollection('roles');

db = db.getSiblingDB('auction_procar');
db.createCollection('auctions');
db.createCollection('bids');

print('MongoDB initialization completed successfully');
