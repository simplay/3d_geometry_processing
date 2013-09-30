#version 150
// Default fragment shader

// Input variable, passed from vertex to fragment shader
// it is interpolated automatically on each fragment
in vec4 frag_color;
in float o_curvature;

// The output variable, will be written to the display automatically
out vec4 out_color;

void main()
{		
	float f = log(1.0 + o_curvature/10.0);
//	out_color = vec4(f-1.0, f, 1.0-f, 1.0);		
	out_color = vec4(f, f, f, 1.0);	
}
