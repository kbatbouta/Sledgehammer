module.exports = function(grunt) {
    var userhome = require('userhome');
    var mods = userhome('zomboid', 'mods') + '/';
    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),
        copy: {
            main: {
                files: [
                	// ### MOD COPY ###
                	// Production folder.
                	{ expand: true, src: 'SledgehammerLua/**', dest: mods           , options: { timestamp: true, mode: true } },
                    // Zomboid USER-HOME folder.
                    { expand: true, src: 'SledgehammerLua/**', dest: '../prod/mods/', options: { timestamp: true, mode: true } }
                	// ################
                ],
            },
        },
        watch: {
            scripts: {
                files: ['src/**'],
                tasks: ['copy'],
                options: {
                    spawn: false,
                },
            },
        },
    });
    grunt.loadNpmTasks('grunt-contrib-copy');
    grunt.loadNpmTasks('grunt-contrib-watch');
    grunt.registerTask('default', ['watch']);
};