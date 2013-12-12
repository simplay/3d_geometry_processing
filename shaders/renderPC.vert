#version 150

uniform mat4 projection; 
uniform mat4 modelview;

in vec4 position;
in vec3 normal;

out vec4 frag_color;

void main() {
	// color is passed in the normal variable
	frag_color = vec4(normal.x, normal.y, normal.z, 1);
	
	gl_Position = projection * modelview * position;
}
