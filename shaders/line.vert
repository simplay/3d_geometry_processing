#version 150

uniform mat4 projection; 
uniform mat4 modelview;

in vec4 position;
in vec3 color;

out vec4 frag_color;

void main()
{
	frag_color = vec4(color.x, color.y, color.z, 1);
	gl_Position = projection * modelview * position;
}
