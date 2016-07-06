module.exports = function(grunt) {

	// Project configuration.
	grunt.initConfig({
		pkg: grunt.file.readJSON('package.json'),
		replace: {
			local: {
				options: {
					patterns: [{
						json: grunt.file.readJSON('./config/local.json')
					}]
				},
				files: [{
					expand: true,
					flatten: true,
					src: ['./config/config.js'],
					dest: './app/views/main/'
				}]
			},
			development: {
				options: {
					patterns: [{
						json: grunt.file.readJSON('./config/development.json')
					}]
				},
				files: [{
					expand: true,
					flatten: true,
					src: ['./config/config.js'],
					dest: './app/views/main/'
				}]
			},
			production: {
				options: {
					patterns: [{
						json: grunt.file.readJSON('./config/production.json')
					}]
				},
				files: [{
					expand: true,
					flatten: true,
					src: ['./config/config.js'],
					dest: './app/views/main/'
				}]
			}
		}
	});

	// Load the plugin that provides the "replace" task.
	grunt.loadNpmTasks('grunt-replace');

	// Default task(s).
	grunt.registerTask('default', ['replace']);

};