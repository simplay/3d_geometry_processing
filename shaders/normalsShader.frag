#version 150
// Default fragment shader

// Input variable, passed from vertex to fragment shader
// it is interpolated automatically on each fragment
in vec4 frag_color;
in vec3 normal_o;
// The output variable, will be written to the display automatically
out vec4 out_color;

void main()
{		
	vec3 other = vec3(1.0, 1.0, 1.0);
	out_color = frag_color*(abs(dot(normal_o, other)));	
}
