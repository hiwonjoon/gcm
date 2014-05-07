class Player
  constructor: (@name, @sprite) ->
  x: 0
  y: 0

class Npc
  x: 0
  y: 0

maleNames = ['Major', 'Gael', 'Jase', 'Messiah', 'Brantley', 'Iker', 'King', 'Rory', 'Ari', 'Maverick', 'Armani', 'Knox', 'Gianni', 'Zayden', 'August', 'Barrett', 'Remington', 'Kasen', 'Zaiden', 'Orion', 'Atticus', 'Leon', 'Abram', 'Ryker', 'Gunnar', 'Waylon', 'Lincoln', 'Bruce', 'Abel', 'Kendrick', 'Karter', 'Everett', 'Archer', 'Graham', 'Damian', 'Cyrus', 'Cason', 'Gunner', 'Bennett', 'Beau', 'Romeo', 'Noel', 'Luca', 'Ahmed', 'Royce', 'Arjun', 'Jaxson', 'Emmett', 'Weston', 'Theodore', 'Sullivan', 'Mateo', 'Leo', 'Declan', 'Paxton', 'Kason', 'Malcolm', 'Chris', 'Milo', 'Elliot', 'Gustavo', 'Zachariah', 'Phillip', 'Elliott', 'Braxton', 'Warren', 'Kolton', 'Greyson', 'Sawyer', 'Elias', 'Jayce', 'Dean', 'Prince', 'Jamari', 'Emerson', 'Silas', 'Dexter', 'Orlando', 'Porter', 'Louis', 'Jaxon', 'Jace', 'Gideon', 'Khalil', 'Hudson', 'Ezra', 'Davis', 'Landyn', 'Kingston', 'Johan', 'Calvin', 'Ali', 'Lukas', 'Jasper', 'Harrison', 'Zander', 'River', 'Maxwell', 'Leonel', 'Jonas', 'Desmond', 'Cruz', 'Beckett', 'Myles', 'Jeremy', 'Ivan', 'Alexis', 'Muhammad', 'Judah', 'Jax', 'Henry', 'Eli', 'Avery', 'Anderson', 'Jonah', 'Jay', 'Steven', 'Grayson', 'Tucker', 'Rhys', 'Marco', 'Levi', 'Kamden', 'Julius', 'Finn', 'Zion', 'Hunter', 'Bradley', 'Santiago', 'Liam', 'Keegan', 'Damon', 'Colton', 'Ryder', 'Preston', 'Jeffrey', 'Wyatt', 'Victor', 'Rowan', 'Phoenix', 'Kellen', 'Keith', 'Kaiden', 'Kai', 'Griffin', 'Emilio', 'Dominick', 'Ramon', 'Owen', 'Nehemiah', 'Maximus', 'Kayden', 'Easton', 'Dominic', 'Walker', 'Titus', 'Reid', 'Oliver', 'Isaac', 'Carter', 'Asher', 'Tanner', 'Solomon', 'Sebastian', 'Nolan', 'Miles', 'Lorenzo', 'Ethan', 'Dallas', 'Camden', 'Bryson', 'Alijah', 'Timothy', 'Richard', 'Karson', 'Joaquin', 'Jameson', 'James', 'Hugo', 'Holden', 'Drew', 'Charlie', 'Benjamin', 'Amir', 'Maddox', 'Luke', 'Lucas', 'Joseph', 'Dylan', 'Blake', 'Austin', 'Andy', 'Wesley', 'Tate', 'Stephen', 'Samuel', 'Ronan', 'Robert', 'Patrick', 'Noah', 'Marvin', 'Lawrence', 'Josiah', 'Johnny', 'Jackson', 'Isaiah', 'Dawson', 'Cohen', 'Ayden', 'Thomas', 'Simon', 'Nathaniel', 'Micah', 'Matthew', 'Mason', 'Landon', 'Kevin', 'Jacob', 'Gabriel', 'Elijah', 'Caleb', 'Bentley', 'Antonio', 'Adriel', 'Walter', 'Ryan', 'Parker', 'Nathan', 'Logan', 'Leonardo', 'John', 'Jeremiah', 'Jack', 'George', 'Derek', 'David', 'Daniel', 'Cooper', 'Colt', 'Charles', 'Cameron', 'Brady', 'Alexander', 'Aiden', 'Adam', 'Zane', 'William', 'Tristan', 'Skyler', 'Roman', 'Michael', 'Luis', 'Jordan', 'Jamison', 'Giovanni', 'Clayton', 'Christopher', 'Carson', 'Brayden', 'Andrew', 'Abraham', 'Aaron', 'Vincent', 'Martin', 'Julian', 'Juan', 'Joshua', 'Jorge', 'Jayden', 'Ezekiel', 'Eric', 'Colin', 'Christian', 'Bryce', 'Amari', 'Alex', 'Zachary', 'Xavier', 'Philip', 'Miguel', 'Marcus', 'Malachi', 'Jonathan', 'Gavin', 'Erick', 'Dustin', 'Curtis', 'Carlos', 'Anthony', 'Adrian', 'Sergio', 'Reed', 'Kenneth', 'Jason', 'Connor', 'Angel', 'Tristen', 'Trent', 'Ruben', 'Ricky', 'Pierce', 'Paul', 'Omar', 'Marcos', 'Kyle', 'Kameron', 'Ismael', 'Ian', 'Felix', 'Cash', 'Nicholas', 'Jude', 'Jose', 'Fabian', 'Evan', 'Alan', 'Spencer', 'Peter', 'Marshall', 'Jesus', 'Izaiah', 'Gregory', 'Dillon', 'Dane', 'Cole', 'Chase', 'Brody', 'Alfredo', 'Albert', 'Aidan', 'Taylor', 'Rodrigo', 'Mohamed', 'Max', 'Kaden', 'Esteban', 'Edward', 'Darren', 'Caiden', 'Braylen', 'Brandon', 'Pedro', 'Pablo', 'Kristopher', 'Kellan', 'Kaleb', 'Jesse', 'Frank', 'Drake', 'Dalton', 'Allen', 'Aden', 'Xander', 'Diego', 'Dante', 'Bryan', 'Brock', 'Tyson', 'Tyler', 'Ricardo', 'Rafael', 'Nicolas', 'Joel', 'Grant', 'Erik', 'Derrick', 'Angelo', 'Andre', 'Roberto', 'Nikolas', 'Jett', 'Gerardo', 'Finnegan', 'Corbin', 'Brooks', 'Andres', 'Raymond', 'Leland', 'Kyler', 'Dennis', 'Cody', 'Chandler', 'Cayden', 'Troy', 'Oscar', 'Manuel', 'Justin', 'Jaime', 'Francisco', 'Enzo', 'Enrique', 'Devin', 'Tony', 'Rocco', 'Mathew', 'Caden', 'Scott', 'Saul', 'Peyton', 'Mark', 'Lane', 'Javier', 'Issac', 'Brian', 'Arthur', 'Malik', 'Johnathan', 'Gage', 'Emmanuel', 'Alejandro', 'Sean', 'Moises', 'Hayden', 'Emiliano', 'Damien', 'Colten', 'Adan', 'Landen', 'Hector', 'Eduardo', 'Dakota', 'Brycen', 'Travis', 'Julio', 'Jaiden', 'Ibrahim', 'Donovan', 'Darius', 'Trenton', 'Riley', 'Mekhi', 'Jayson', 'Trevor', 'Russell', 'Kade', 'Garrett', 'Donald', 'Collin', 'Cade', 'Armando', 'Shawn', 'Seth', 'Josue', 'Jake', 'Chance', 'Ryland', 'Quinn', 'Edgar', 'Brennan', 'Conner', 'Axel', 'Alberto', 'Matteo', 'Braydon', 'Arturo', 'Alec', 'Mauricio', 'Larry', 'Cristian', 'Ty', 'Lance', 'Kobe', 'Jared', 'Grady', 'Colby', 'Maximiliano', 'Mario', 'Keaton', 'Nico', 'Maximilian', 'Ashton', 'Shane', 'Sam', 'Rylan', 'Israel', 'Quentin', 'Fernando', 'Danny', 'Uriel', 'Cesar', 'Bryant', 'Jacoby', 'Edwin', 'Brenden', 'Brendan', 'Ronald', 'Joe', 'Gary', 'Corey', 'Jaden', 'Jimmy', 'Raul', 'Maurice', 'Mitchell', 'Reece', 'Emanuel', 'Braden', 'Devon', 'Braylon', 'Ernesto', 'Jaylen', 'Jerry', 'Zackary', 'Eddie', 'Randy', 'Payton', 'Jakob', 'Casey', 'Trey', 'Jalen', 'Amare', 'Brayan', 'Cullen', 'Kieran', 'Yahir', 'Braeden']
sprites = ['08sprite', '11sprite', '12sprite', '13sprite', '15sprite', '16sprite', '17sprite', '18sprite']

module.exports = (server) ->

  playerTable = {}

  io = require('socket.io').listen server
  io.on 'connection', (socket) ->
    console.log socket.id

    ###
        i = Math.floor Math.random() * maleNames.length
        j = Math.floor Math.random() * sprites.length
        myName = maleNames[i]
        mySprite = sprites[j]
        playerTable[socket.id] = new Player myName, mySprite
        maleNames.splice i, 1
        socket.emit 'sConnection', { name: myName, sprite: mySprite }
    ###

    socket.on 'cMove', (data) ->
      player = playerTable[socket.id]
      player.x = data.x
      player.y = data.y
      socket.broadcast.emit 'sMove', { name: player.name, sprite: player.sprite, x: data.x, y: data.y }

    socket.on 'cAttack', (content) ->

    socket.on 'cLogin', (data) ->
      playerTable[socket.id] = new Player data.name, data.sprite
      socket.emit 'sLogin', { name: data.name, sprite: data.sprite }
      for k, v of playerTable
        socket.emit 'sMove', { name: v.name, sprite: v.sprite, x: v.x, y: v.y } if v.name != data.name

    socket.on 'cChat', (content) ->
      player = playerTable[socket.id]
      socket.emit 'sChat', 'You said: ' + content
      socket.broadcast.emit 'sChat', player.name + ' said: ' + content

    socket.on 'disconnect', () ->
      player = playerTable[socket.id]
      #maleNames.push player.name
      socket.broadcast.emit 'sQuit', player.name

      delete playerTable[socket.id]

